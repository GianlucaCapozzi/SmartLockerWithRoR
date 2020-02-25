class AuthorizeApiRequest
    prepend SimpleCommand
  
    def initialize(headers = {})
        @headers = headers
    end
  
    def call
        user
    end

    public 
    
    def check_token(token)
        @decoded_token = decoded_auth_token(token)
        if @decoded_token[:exp] >= Time.now.to_i
            errors.add(:token, 'Token expired, please login again')
            return false
        elsif BlacklistedToken.exists?(token: @decoded_token)
            errors.add(:token, 'Token in blacklist, please login again')
            return false
        else
            return true
    end
  
    private
  
    attr_reader :headers
  
    def user
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
        @user || errors.add(:token, 'Invalid token') && nil
    end
  
    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken.decode(http_auth_header)
    end
  
    def http_auth_header
        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end